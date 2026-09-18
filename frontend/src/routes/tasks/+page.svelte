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
		<div class="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
			<div>
				<h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">
					Public Task Directory
				</h1>
				<p class="mt-1 text-sm text-slate-600">
					Explore live SME tasks funded by corporate reserves with guaranteed compensation.
				</p>
			</div>

			<div class="flex items-center gap-2">
				<button
					type="button"
					class="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors shadow-2xs"
					onclick={loadTasks}
				>
					<svg class="h-3.5 w-3.5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
					</svg>
					<span>Refresh</span>
				</button>
			</div>
		</div>

		<!-- Filter Bar -->
		<div class="rounded-2xl border border-slate-200/80 bg-white p-4 shadow-xs mb-8 space-y-4">
			<div class="grid grid-cols-1 md:grid-cols-4 gap-4">
				<!-- Search -->
				<div class="md:col-span-2">
					<label for="searchTasks" class="sr-only">Search tasks</label>
					<div class="relative">
						<div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
							<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
							</svg>
						</div>
						<input
							id="searchTasks"
							type="text"
							bind:value={searchQuery}
							placeholder="Search by task title, keywords, or SME name..."
							class="block w-full rounded-xl border border-slate-300 pl-9 pr-4 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
						/>
					</div>
				</div>

				<!-- Category Select -->
				<div>
					<label for="categoryFilter" class="sr-only">Filter category</label>
					<select
						id="categoryFilter"
						bind:value={selectedCategory}
						class="block w-full rounded-xl border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
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
						class="block w-full rounded-xl border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
					>
						{#each statuses as st}
							<option value={st}>{st === 'ALL' ? 'All Statuses' : st.replace('_', ' ')}</option>
						{/each}
					</select>
				</div>
			</div>

			<!-- Active filter counts -->
			<div class="flex items-center justify-between text-xs text-slate-500 pt-2 border-t border-slate-100">
				<span>Showing {filteredTasks.length} of {tasks.length} tasks</span>
				{#if searchQuery || selectedCategory !== 'ALL' || selectedStatus !== 'ALL'}
					<button
						type="button"
						class="font-semibold text-indigo-600 hover:text-indigo-800"
						onclick={() => {
							searchQuery = '';
							selectedCategory = 'ALL';
							selectedStatus = 'ALL';
						}}
					>
						Reset filters
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
					<div class="h-64 rounded-2xl bg-slate-200/60 animate-pulse"></div>
				{/each}
			</div>
		{:else if filteredTasks.length === 0}
			<div class="rounded-3xl border border-dashed border-slate-300 bg-white p-12 text-center">
				<svg class="mx-auto h-12 w-12 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
				</svg>
				<h3 class="mt-2 text-sm font-semibold text-slate-900">No tasks matched your criteria</h3>
				<p class="mt-1 text-xs text-slate-500">Try adjusting your search terms or filters.</p>
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
