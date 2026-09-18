<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import TaskCard from '$lib/components/TaskCard.svelte';
	import ApiError from '$lib/components/ApiError.svelte';
	import { api } from '$lib/api.js';
	import { onMount } from 'svelte';

	let tasks = $state([]);
	let loading = $state(true);
	let errorMessage = $state(null);

	// Filters
	let searchQuery = $state('');
	let selectedCategory = $state('ALL');
	let selectedStatus = $state('ALL');

	const categories = [
		'ALL',
		'Digital Operations',
		'Marketing & Creative',
		'Accounting & Finance',
		'IT & Technical',
		'Customer Support'
	];

	const statuses = [
		'ALL',
		'OPEN',
		'IN_PROGRESS',
		'COMPLETED',
		'VERIFIED'
	];

	async function loadTasks() {
		loading = true;
		errorMessage = null;
		try {
			const res = await api.tasks.list();
			tasks = Array.isArray(res) ? res : [];
		} catch (err) {
			console.error('Failed to load tasks:', err);
			errorMessage = err.message || 'Could not load tasks from hub.';
		} finally {
			loading = false;
		}
	}

	const filteredTasks = $derived.by(() => {
		return tasks.filter((t) => {
			const matchesCategory =
				selectedCategory === 'ALL' ||
				(t.category && t.category.toLowerCase() === selectedCategory.toLowerCase());

			const matchesStatus =
				selectedStatus === 'ALL' ||
				(t.status && t.status.toUpperCase() === selectedStatus.toUpperCase());

			const query = searchQuery.trim().toLowerCase();
			const matchesSearch =
				!query ||
				(t.title && t.title.toLowerCase().includes(query)) ||
				(t.description && t.description.toLowerCase().includes(query)) ||
				(t.smeBusinessName && t.smeBusinessName.toLowerCase().includes(query));

			return matchesCategory && matchesStatus && matchesSearch;
		});
	});

	onMount(() => {
		loadTasks();
	});
</script>

<svelte:head>
	<title>Task Directory | Shared Services Hub</title>
</svelte:head>

<RoleGuard requireAuth={true}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Page Header -->
		<div class="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4 border-b border-[#C8D4E8] pb-6">
			<div>
				<h1 class="font-serif text-4xl text-[#0D1B3E] font-normal tracking-tight">
					Public Task Directory
				</h1>
				<p class="mt-1 text-sm text-[#465A7A] font-sans">
					Live SME tasks backed by corporate funding with guaranteed compensation.
				</p>
			</div>

			<div class="flex items-center gap-2">
				<button
					type="button"
					class="border border-[#C8D4E8] bg-white px-3.5 py-2 text-xs font-mono uppercase tracking-wider text-[#0D1B3E] hover:bg-[#F8F9FC] transition-colors"
					onclick={loadTasks}
				>
					↻ Refresh Directory
				</button>
			</div>
		</div>

		<!-- Filter Bar (Sharp, Cape Cobalt) -->
		<div class="border border-[#C8D4E8] bg-white p-4 shadow-xs mb-8 space-y-4">
			<div class="grid grid-cols-1 md:grid-cols-4 gap-4">
				<!-- Search -->
				<div class="md:col-span-2">
					<label for="searchTasks" class="sr-only">Search tasks</label>
					<input
						id="searchTasks"
						type="text"
						bind:value={searchQuery}
						placeholder="Search by task title, keywords, or SME name..."
						class="cape-input block w-full px-4 py-2 text-sm"
					/>
				</div>

				<!-- Category Select -->
				<div>
					<label for="categoryFilter" class="sr-only">Filter category</label>
					<select
						id="categoryFilter"
						bind:value={selectedCategory}
						class="cape-input block w-full px-3 py-2 text-sm"
					>
						{#each categories as cat}
							<option value={cat}>{cat === 'ALL' ? 'All Categories' : cat}</option>
						{/each}
					</select>
				</div>

				<!-- Status Select -->
				<div>
					<label for="statusFilter" class="sr-only">Filter status</label>
					<select
						id="statusFilter"
						bind:value={selectedStatus}
						class="cape-input block w-full px-3 py-2 text-sm"
					>
						{#each statuses as st}
							<option value={st}>{st === 'ALL' ? 'All Statuses' : st.replace('_', ' ')}</option>
						{/each}
					</select>
				</div>
			</div>

			<!-- Active filter counts -->
			<div class="flex items-center justify-between text-xs font-mono text-[#6A84AC] pt-2 border-t border-[#C8D4E8]">
				<span>Showing {filteredTasks.length} of {tasks.length} tasks</span>
				{#if searchQuery || selectedCategory !== 'ALL' || selectedStatus !== 'ALL'}
					<button
						type="button"
						class="font-bold text-[#1446A0] hover:underline"
						onclick={() => {
							searchQuery = '';
							selectedCategory = 'ALL';
							selectedStatus = 'ALL';
						}}
					>
						[ Reset filters ]
					</button>
				{/if}
			</div>
		</div>

		<!-- Error Banner -->
		<ApiError message={errorMessage} onretry={loadTasks} />

		<!-- Task Grid -->
		{#if loading}
			<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
				{#each Array(6) as _}
					<div class="h-64 border border-[#C8D4E8] bg-white animate-pulse"></div>
				{/each}
			</div>
		{:else if filteredTasks.length === 0}
			<div class="border border-dashed border-[#C8D4E8] bg-white p-12 text-center">
				<h3 class="font-serif text-lg text-[#0D1B3E]">No tasks found</h3>
				<p class="mt-1 text-xs text-[#6A84AC]">Try broadening your filter criteria.</p>
			</div>
		{:else}
			<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
				{#each filteredTasks as task (task.id)}
					<TaskCard {task} />
				{/each}
			</div>
		{/if}
	</div>
</RoleGuard>
