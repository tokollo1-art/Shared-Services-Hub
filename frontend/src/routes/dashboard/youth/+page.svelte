<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import TaskCard from '$lib/components/TaskCard.svelte';
	import ExperienceCard from '$lib/components/ExperienceCard.svelte';
	import ApiError from '$lib/components/ApiError.svelte';
	import { api } from '$lib/api.js';
	import { currentUser } from '$lib/stores/auth.js';
	import { formatCurrency } from '$lib/utils/formatters.js';
	import { onMount } from 'svelte';

	const user = $derived($currentUser);

	let tasks = $state([]);
	let experiences = $state([]);
	let loadingTasks = $state(true);
	let loadingExp = $state(true);
	let errorMessage = $state(null);

	const isAlumni = $derived(user?.isAlumni || false);

	// Summary stats
	const verifiedExperiences = $derived(experiences.filter((e) => e.isVerified));
	const averageRating = $derived.by(() => {
		const rated = verifiedExperiences.filter((e) => e.rating);
		if (rated.length === 0) return '5.0';
		const sum = rated.reduce((acc, curr) => acc + Number(curr.rating), 0);
		return (sum / rated.length).toFixed(1);
	});

	const totalEarned = $derived.by(() => {
		return experiences.length * 1100;
	});

	async function loadDashboardData() {
		errorMessage = null;

		loadingTasks = true;
		try {
			const res = await api.tasks.list();
			tasks = Array.isArray(res) ? res : [];
		} catch (err) {
			console.error('Failed to load tasks:', err);
			errorMessage = err.message || 'Could not load tasks.';
		} finally {
			loadingTasks = false;
		}

		const youthId = user?.id || 1;
		loadingExp = true;
		try {
			const res = await api.experiences.listForYouth(youthId);
			experiences = Array.isArray(res) ? res : [];
		} catch (err) {
			console.error('Failed to load experience ledger:', err);
		} finally {
			loadingExp = false;
		}
	}

	onMount(() => {
		loadDashboardData();
	});
</script>

<svelte:head>
	<title>Youth Candidate Dashboard | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={['YOUTH', 'ADMIN']}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header (Cape Cobalt Surface-Dark #0A1E4A) -->
		<div class="border border-[#C8D4E8] bg-[#0A1E4A] p-8 text-white shadow-md mb-8">
			<div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
				<div>
					<div class="flex items-center gap-3 flex-wrap">
						<h1 class="font-serif text-3xl sm:text-4xl font-normal text-white">
							Welcome, {user?.name || 'Candidate'}
						</h1>
						{#if isAlumni}
							<span class="inline-block bg-[#1A8A55] text-white px-2 py-0.5 font-mono text-[10px] font-bold uppercase tracking-widest">
								PERMANENT ALUMNI
							</span>
						{:else}
							<span class="inline-block bg-[#1446A0] text-white px-2 py-0.5 font-mono text-[10px] font-bold uppercase tracking-widest border border-white/20">
								ACTIVE CANDIDATE
							</span>
						{/if}
					</div>
					<p class="mt-2 text-sm text-[#C8D4E8] max-w-xl font-light font-sans">
						Complete verified tasks for local SMEs to expand your Experience Ledger and earn corporate-funded wages.
					</p>
				</div>

				<div class="flex items-center gap-3">
					<a
						href="/tasks"
						class="bg-white text-[#0D1B3E] px-4 py-2.5 font-mono text-xs font-bold uppercase tracking-wider hover:bg-[#EFF4FF] transition-colors"
					>
						Browse All Tasks
					</a>
				</div>
			</div>

			<!-- Stat Counters (Sharp, Editorial) -->
			<div class="mt-8 pt-6 border-t border-[#1C3565] grid grid-cols-2 sm:grid-cols-4 gap-4">
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Available Tasks</div>
					<div class="font-mono text-2xl font-bold mt-1 text-white">{tasks.filter(t => t.status === 'OPEN').length}</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Verified Records</div>
					<div class="font-mono text-2xl font-bold mt-1 text-[#7C3FE4]">{verifiedExperiences.length}</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Average Rating</div>
					<div class="font-mono text-2xl font-bold mt-1 text-[#E8B830]">
						★ {averageRating}
					</div>
				</div>
				<div>
					<div class="font-mono text-[11px] font-bold uppercase tracking-wider text-[#6A84AC]">Total Paid</div>
					<div class="font-mono text-2xl font-bold mt-1 text-[#1A8A55]">{formatCurrency(totalEarned)}</div>
				</div>
			</div>
		</div>

		<!-- Error Banner -->
		<ApiError message={errorMessage} onretry={loadDashboardData} />

		<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
			<!-- Left Column (2 Cols): Open Tasks -->
			<div class="lg:col-span-2 space-y-6">
				<div class="flex items-center justify-between border-b border-[#C8D4E8] pb-3">
					<div>
						<h2 class="font-serif text-2xl text-[#0D1B3E]">Open Tasks</h2>
						<p class="text-xs text-[#6A84AC] font-sans">Immediate SME tasks with guaranteed corporate funding.</p>
					</div>
					<a href="/tasks" class="font-mono text-xs font-bold uppercase tracking-wider text-[#1446A0] hover:underline">
						View All ({tasks.length}) →
					</a>
				</div>

				{#if loadingTasks}
					<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
						{#each Array(4) as _}
							<div class="h-48 border border-[#C8D4E8] bg-white animate-pulse"></div>
						{/each}
					</div>
				{:else if tasks.length === 0}
					<div class="border border-dashed border-[#C8D4E8] bg-white p-12 text-center">
						<h3 class="font-serif text-lg text-[#0D1B3E]">No open tasks available</h3>
						<p class="mt-1 text-xs text-[#6A84AC]">Please check back shortly as new SME tasks are posted daily.</p>
					</div>
				{:else}
					<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
						{#each tasks.slice(0, 6) as task (task.id)}
							<TaskCard {task} />
						{/each}
					</div>
				{/if}
			</div>

			<!-- Right Column (1 Col): My Experience Ledger -->
			<div class="space-y-6">
				<div class="flex items-center justify-between border-b border-[#C8D4E8] pb-3">
					<div>
						<h2 class="font-serif text-2xl text-[#0D1B3E]">Experience Ledger</h2>
						<p class="text-xs text-[#6A84AC] font-sans">Your permanent record of capability.</p>
					</div>
					<span class="bg-[#EFF4FF] border border-[#C8D4E8] px-2 py-0.5 text-[10px] font-mono font-bold text-[#1446A0]">
						{experiences.length} ENTRIES
					</span>
				</div>

				{#if loadingExp}
					<div class="space-y-4">
						<div class="h-36 border border-[#C8D4E8] bg-white animate-pulse"></div>
						<div class="h-36 border border-[#C8D4E8] bg-white animate-pulse"></div>
					</div>
				{:else if experiences.length === 0}
					<div class="border border-dashed border-[#C8D4E8] bg-white p-8 text-center">
						<h3 class="font-serif text-lg text-[#0D1B3E]">No entries recorded</h3>
						<p class="mt-1 text-xs text-[#6A84AC] leading-relaxed">
							Complete your first SME task to generate a verified proof entry.
						</p>
					</div>
				{:else}
					<div class="space-y-4">
						{#each experiences as exp (exp.id)}
							<ExperienceCard experience={exp} />
						{/each}
					</div>
				{/if}
			</div>
		</div>
	</div>
</RoleGuard>
